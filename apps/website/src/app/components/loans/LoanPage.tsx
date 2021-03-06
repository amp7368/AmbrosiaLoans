import { Loan, LoanSimple } from '@api/io-model';
import { useObservableList } from '@appleptr16/elemental';
import {
    alpha,
    Button,
    colors,
    Container,
    List,
    ListItem,
    Stack,
    Table,
    TableHead,
    Typography,
} from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { map } from 'rxjs';
import { loanQuery } from '../../akita/loan/Loan.query';

import { routes } from '../../util/routes';
import { AppPaper } from '../common/AppPaper';
import { EmeraldDisplay, EmeraldDisplayHeader } from '../common/EmeraldDisplay';
import { Page } from '../common/Page';

function LoanRow(Loan: LoanSimple) {
    return (
        <ListItem>
            <Stack direction="row" key={Loan.uuid}>
                <Typography>{Loan.broker}</Typography>
                <Typography>{Loan.currentLoan}</Typography>
            </Stack>
        </ListItem>
    );
}
const columns: GridColDef[] = [
    { field: 'broker', headerName: 'Broker', width: 100 },
    {
        field: 'currentLoan',
        headerName: 'Current Loan',
        type: 'number',
        width: 200,
        renderCell: (ems) => {
            return <EmeraldDisplay emeralds={ems.value} />;
        },
        renderHeader: EmeraldDisplayHeader,
    },
    {
        field: 'rate',
        headerName: 'Rate %',
        width: 100,
        valueFormatter: (rate) => `${rate.value}%`,
    },
];

export function LoansPage() {
    const Loans = loanQuery.loans
        .select()
        .pipe(map((state) => state.newState ?? []));

    const loans = useObservableList(Loans);
    return (
        <>
            <Page title="Loans">
                <Stack
                    direction="column"
                    bgcolor={alpha(colors.common.black, 0.4)}
                >
                    <DataGrid
                        rowSpacingType="margin"
                        autoHeight
                        rows={loans}
                        getRowId={(row: LoanSimple) => row.uuid}
                        columns={columns}
                    />
                    <Container>
                        <Button
                            variant="outlined"
                            color="secondary"
                            href={routes.createLoan}
                        >
                            +
                        </Button>
                    </Container>
                </Stack>
            </Page>
        </>
    );
}
